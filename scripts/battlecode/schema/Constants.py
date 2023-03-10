# automatically generated by the FlatBuffers compiler, do not modify

# namespace: schema

import flatbuffers
from flatbuffers.compat import import_numpy
np = import_numpy()

class Constants(object):
    __slots__ = ['_tab']

    @classmethod
    def GetRootAs(cls, buf, offset=0):
        n = flatbuffers.encode.Get(flatbuffers.packer.uoffset, buf, offset)
        x = Constants()
        x.Init(buf, n + offset)
        return x

    @classmethod
    def GetRootAsConstants(cls, buf, offset=0):
        """This method is deprecated. Please switch to GetRootAs."""
        return cls.GetRootAs(buf, offset)
    # Constants
    def Init(self, buf, pos):
        self._tab = flatbuffers.table.Table(buf, pos)

    # Constants
    def IncreasePeriod(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        if o != 0:
            return self._tab.Get(flatbuffers.number_types.Int32Flags, o + self._tab.Pos)
        return 0

    # Constants
    def AdAdditiveIncrease(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        if o != 0:
            return self._tab.Get(flatbuffers.number_types.Int32Flags, o + self._tab.Pos)
        return 0

    # Constants
    def MnAdditiveIncrease(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(8))
        if o != 0:
            return self._tab.Get(flatbuffers.number_types.Int32Flags, o + self._tab.Pos)
        return 0

def ConstantsStart(builder): builder.StartObject(3)
def Start(builder):
    return ConstantsStart(builder)
def ConstantsAddIncreasePeriod(builder, increasePeriod): builder.PrependInt32Slot(0, increasePeriod, 0)
def AddIncreasePeriod(builder, increasePeriod):
    return ConstantsAddIncreasePeriod(builder, increasePeriod)
def ConstantsAddAdAdditiveIncrease(builder, adAdditiveIncrease): builder.PrependInt32Slot(1, adAdditiveIncrease, 0)
def AddAdAdditiveIncrease(builder, adAdditiveIncrease):
    return ConstantsAddAdAdditiveIncrease(builder, adAdditiveIncrease)
def ConstantsAddMnAdditiveIncrease(builder, mnAdditiveIncrease): builder.PrependInt32Slot(2, mnAdditiveIncrease, 0)
def AddMnAdditiveIncrease(builder, mnAdditiveIncrease):
    return ConstantsAddMnAdditiveIncrease(builder, mnAdditiveIncrease)
def ConstantsEnd(builder): return builder.EndObject()
def End(builder):
    return ConstantsEnd(builder)