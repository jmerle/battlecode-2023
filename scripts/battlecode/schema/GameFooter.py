# automatically generated by the FlatBuffers compiler, do not modify

# namespace: schema

import flatbuffers
from flatbuffers.compat import import_numpy
np = import_numpy()

# The final event sent in the game.
class GameFooter(object):
    __slots__ = ['_tab']

    @classmethod
    def GetRootAs(cls, buf, offset=0):
        n = flatbuffers.encode.Get(flatbuffers.packer.uoffset, buf, offset)
        x = GameFooter()
        x.Init(buf, n + offset)
        return x

    @classmethod
    def GetRootAsGameFooter(cls, buf, offset=0):
        """This method is deprecated. Please switch to GetRootAs."""
        return cls.GetRootAs(buf, offset)
    # GameFooter
    def Init(self, buf, pos):
        self._tab = flatbuffers.table.Table(buf, pos)

    # The ID of the winning team of the game.
    # GameFooter
    def Winner(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        if o != 0:
            return self._tab.Get(flatbuffers.number_types.Int8Flags, o + self._tab.Pos)
        return 0

def GameFooterStart(builder): builder.StartObject(1)
def Start(builder):
    return GameFooterStart(builder)
def GameFooterAddWinner(builder, winner): builder.PrependInt8Slot(0, winner, 0)
def AddWinner(builder, winner):
    return GameFooterAddWinner(builder, winner)
def GameFooterEnd(builder): return builder.EndObject()
def End(builder):
    return GameFooterEnd(builder)