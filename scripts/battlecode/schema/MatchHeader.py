# automatically generated by the FlatBuffers compiler, do not modify

# namespace: schema

import flatbuffers
from flatbuffers.compat import import_numpy
np = import_numpy()

# Sent to start a match.
class MatchHeader(object):
    __slots__ = ['_tab']

    @classmethod
    def GetRootAs(cls, buf, offset=0):
        n = flatbuffers.encode.Get(flatbuffers.packer.uoffset, buf, offset)
        x = MatchHeader()
        x.Init(buf, n + offset)
        return x

    @classmethod
    def GetRootAsMatchHeader(cls, buf, offset=0):
        """This method is deprecated. Please switch to GetRootAs."""
        return cls.GetRootAs(buf, offset)
    # MatchHeader
    def Init(self, buf, pos):
        self._tab = flatbuffers.table.Table(buf, pos)

    # The map the match was played on.
    # MatchHeader
    def Map(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        if o != 0:
            x = self._tab.Indirect(o + self._tab.Pos)
            from battlecode.schema.GameMap import GameMap
            obj = GameMap()
            obj.Init(self._tab.Bytes, x)
            return obj
        return None

    # The maximum number of rounds in this match.
    # MatchHeader
    def MaxRounds(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        if o != 0:
            return self._tab.Get(flatbuffers.number_types.Int32Flags, o + self._tab.Pos)
        return 0

def MatchHeaderStart(builder): builder.StartObject(2)
def Start(builder):
    return MatchHeaderStart(builder)
def MatchHeaderAddMap(builder, map): builder.PrependUOffsetTRelativeSlot(0, flatbuffers.number_types.UOffsetTFlags.py_type(map), 0)
def AddMap(builder, map):
    return MatchHeaderAddMap(builder, map)
def MatchHeaderAddMaxRounds(builder, maxRounds): builder.PrependInt32Slot(1, maxRounds, 0)
def AddMaxRounds(builder, maxRounds):
    return MatchHeaderAddMaxRounds(builder, maxRounds)
def MatchHeaderEnd(builder): return builder.EndObject()
def End(builder):
    return MatchHeaderEnd(builder)